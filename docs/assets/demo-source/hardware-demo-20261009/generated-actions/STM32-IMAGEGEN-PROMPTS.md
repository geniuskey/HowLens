# STM32 CN2 점퍼 예제 — 실제 imagegen 프롬프트

작성일: 2026-10-09
대상: 마지막 예시인 **STM32 Nucleo CN2 점퍼 설정**.

[최종 생성 이미지](03-stm32-actions.png)

이 노트는 실제로 도구에 전달한 영문 프롬프트를 기록한다. 첫 호출은 공통 지시문과 STM32 지시문을 연결한 하나의 문자열이었으며, 아래 최초 프롬프트에 연결된 내용 전체를 그대로 담았다. 이후 한 차례 수정 호출을 했다.

## 최초 생성

도구: `image_gen.imagegen`

- `transparent_background`: `false`
- `referenced_image_paths`: 아래 두 이미지, 순서 유지
- `num_last_images_to_include`: 사용하지 않음
- 모델명·출력 크기·seed: 호출 인자로 지정하지 않음

| 입력 순서 | 역할 | 저장소에서 볼 수 있는 참조 이미지 |
|---|---|---|
| 1 | 카드 구성·색상·단계 문구 참고 | [기존 원문 기반 스토리보드](../manual-storyboards/03-stm32/storyboard.png) |
| 2 | MB1136 보드 형상과 CN2 위치의 원문 근거 | [매뉴얼 Figure 7](references/03-stm32/cn2.png) |

실제 호출의 참조 경로:

```text
/Users/jymbook/HowLens/output/pdf/hardware-demo-storyboards/03-stm32/storyboard.png
/Users/jymbook/HowLens/output/pdf/hardware-demo-storyboards/03-stm32/assets/cn2.png
```

실제 `prompt` 문자열:

```text
Create ONE polished Korean hardware instructional storyboard image. This is reference-based generation, NEVER zero-base hardware design. Use the first supplied storyboard only for navy header / pale blue numbered card / amber caution / white background visual design and Korean step text. Use the supplied manufacturer figures/photo as hard geometric references for all hardware. Generate genuinely different ACTION illustrations per step: hands, tool contact, direction arrows and intermediate states, not nine copies of the same original. Keep component shapes, holes, fasteners, relative positions, PCB layout and cable routing consistent with reference. Hand/tool additions are illustrative, not real work photos. Label footer '매뉴얼 참조 기반 AI 설명 이미지 · 실제 작업 사진 아님 · 형상·동작 검토 필요'. Source citations small in each card using original PDF pages from supplied storyboard. Legible Korean text, no cropped cards, no fabricated numeric readings or safety success. Preserve clear step sequence. If a detail is not visible, keep it schematic or offscreen instead of inventing equipment. Landscape-ish 3 columns x 2 rows SIX numbered cards, matched visual design. Title 'STM32 Nucleo CN2 점퍼 설정'. Use the exact MB1136 Nucleo top-down board diagram reference: USB mini-B at top, ST-LINK top region, lower target MCU board, two large morpho headers down the sides, 2 small CN2 jumper caps in a vertical pair near upper-left of top ST-LINK area. CN4 is a separate vertical SWD header LEFT of CN2; never touch CN4. Do not reinterpret CN2 as the long side headers. Preserve all traces/chips/holes/connectors and pink reference location. Panels:1 '보드·목적 확인' full exact board and label on-board STM32. 2 '취급 조건 확인' same board with USB cable visibly disconnected from exact top USB port, ESD-safe setup, hands off contact pins. 3 'CN2 위치 식별' closeup only exact CN2 two positions circled; CN4 labelled separately excluded. 4 '첫 번째 점퍼 장착' reference-based closeup showing fingertips inserting first tiny black cap onto one of the two actual CN2 pin pairs, other cap absent; no fingers on CN4, no new pins. 5 '두 번째 점퍼 장착' fingertips inserting remaining cap onto other actual CN2 pair; first seated. The two caps remain vertically stacked as original. 6 '도면과 대조·확인' both exact caps seated and inspection magnifier, no USB power, no programming success claim. This insertion order is illustrative, not prescribed by manufacturer. Original Figure7 PDF19; instructions18–19; safety11. Do not add electronics, different board revisions, LEDs glowing, soldering or live electricity.
```

[최초 생성 결과](references/03-stm32/initial-generated-draft.png)

최초 결과의 도구 저장 경로:

```text
/Users/jymbook/Library/Application Support/orca/codex-runtime-home/home/generated_images/01a11ec2-7226-7700-8a45-9309fa7b45fa/exec-96f878c3-b3ea-4e17-b20c-95efbb27fb8c.png
```

## 수정 생성

수정 목적: 이미 장착된 점퍼 위에 다시 삽입하는 표현을 줄이고, 노출된 핀 → 첫 점퍼 장착 → 두 점퍼 장착 상태를 구분하도록 요청했다.

도구·옵션은 최초와 동일하다. 참조 이미지는 다음 두 개로 변경했다.

| 입력 순서 | 역할 | 참조 이미지 |
|---|---|---|
| 1 | 수정 대상 | [최초 생성 결과](references/03-stm32/initial-generated-draft.png) |
| 2 | 형상·CN2 위치 기준 | [매뉴얼 Figure 7](references/03-stm32/cn2.png) |

실제 호출의 참조 경로:

```text
/Users/jymbook/Library/Application Support/orca/codex-runtime-home/home/generated_images/01a11ec2-7226-7700-8a45-9309fa7b45fa/exec-96f878c3-b3ea-4e17-b20c-95efbb27fb8c.png
/Users/jymbook/HowLens/output/pdf/hardware-demo-storyboards/03-stm32/assets/cn2.png
```

실제 `prompt` 문자열:

```text
Targeted sequential-state correction of FIRST 3x2 STM32 poster. SECOND manufacturer diagram fixes ALL board geometry and exact CN2 location (two jumper caps vertically stacked, upper-left beside ST-LINK / NUCLEO lettering). Preserve complete layout, texts, board shape/holes/connectors/chips/wire positions of original, and cards1,2,3,6. Correct ONLY CN2 states in cards4,5, with minimal shape-preserving edit: Card4 '첫 번째 점퍼 장착': both CN2 pin pairs initially UNCAPPED and exposed; only two short gold pins per pair, same footprint as manufacturer caps. Fingertips hold ONE tiny black 2-pin cap directly above and aligned with UPPER pin pair, lower pair remains exposed. No black cap already seated under the cap being inserted; no duplicate caps; arrows show movement down onto actual pin pair (toward board), not sideways. Card5 '두 번째 점퍼 장착': upper pair has EXACTLY ONE black cap seated; LOWER pair shows two exposed short gold pins; fingertips hold second matching cap just above and aligned with lower pair. Card6 both original cap footprints seated as source. Never insert onto CN4 or long side headers. Do not change any other components. USB in card2 should retain source CN1 USB mini-B port geometry, not Type-C/microUSB. Do not invent a new board revision. Preserve reference-based illustration footer.
```

최종 결과의 도구 저장 경로:

```text
/Users/jymbook/Library/Application Support/orca/codex-runtime-home/home/generated_images/01a11ec2-7226-7700-8a45-9309fa7b45fa/exec-6834cf90-307d-427f-b6bf-dcca27ad2380.png
```

## 재사용과 검토

- 원문 이미지를 실제 참조 입력으로 넣어야 한다. 텍스트 프롬프트만으로 zero-base 생성하지 않는다.
- 첫 프롬프트의 공통 문구에는 “not nine copies”가 남아 있지만 STM32의 실제 요청은 명시적으로 **6카드**다. 원문 기록을 위해 수정하지 않았다.
- 이 프롬프트의 형상 유지 지시가 기계적 정확성의 검증 결과라는 뜻은 아니다. CN2 핀·접점의 세부 일치는 미검증이며 [검토 기록](review.json)에 남겨 두었다.
- 도구 출력은 확률적이므로 같은 프롬프트·참조 이미지가 동일한 결과를 보장하지 않는다.
