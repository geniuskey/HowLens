"""Bounded server-only research; never registers web results as manual evidence."""
from .discovery import DiscoveryProvider, manufacturer_key, model_key

# Exact product identities from the public contract. Smart-UPS is a family, so it
# cannot authorize exact-model reanalysis without a separately reviewed SKU map.
EXACT_CATALOG_IDENTITIES = {
    'server': ('Dell', 'PowerEdge R750'),
    'cobot': ('Universal Robots', 'UR5e'),
}


async def enrich_analysis(provider, initial, question, photo):
    if initial.decision != 'needs_more_information' or initial.warnings:
        return initial
    # Initial analysis (1), label/category (1), search (1), optional reanalysis (1).
    discovery = await DiscoveryProvider(provider).discover(photo)
    initial.steps = []
    if discovery.research is not None:
        initial.observations.append(discovery.research.summary)
        for source in discovery.research.sources:
            initial.observations.append(f'제품 종류 조사 출처: {source.title} — {source.url}')
        initial.missing_information.append('제품 종류는 사진 형태에 따른 가설입니다. 정확한 모델 라벨을 확인해 주세요.')
        return initial
    if discovery.status != 'candidate':
        initial.missing_information.extend(discovery.missing_information)
        return initial
    candidate = discovery.candidates[0]
    identity = EXACT_CATALOG_IDENTITIES.get(initial.device_id)
    matches = identity is not None and (
        manufacturer_key(candidate.manufacturer) == manufacturer_key(identity[0])
        and model_key(candidate.model) == model_key(identity[1]))
    # Trusted registry bootstrap checks version/page/quote/source; research cannot
    # populate it. Exact photo identity and existing reviewed excerpts are both required.
    if not matches or not provider.registry.excerpts(initial.device_id):
        initial.observations.append(f'라벨과 출처에서 확인한 제품 후보: {candidate.manufacturer} {candidate.model}')
        initial.missing_information.append('이 정확한 모델에 대한 검증된 작업 매뉴얼이 없어 작업 단계를 제공할 수 없습니다.')
        return initial
    return await provider._analyze_once(initial.device_id, question, photo,
        verified_product={'manufacturer': candidate.manufacturer, 'model': candidate.model,
                          'source_urls': [source.url for source in candidate.sources]})
