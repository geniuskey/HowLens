"""Local human comparison artifacts; no automatic semantic scores or approval."""
import csv
import html
from pathlib import Path


def write_comparison_sheet(root, rows, artifacts):
    groups = {}
    for row in rows:
        groups.setdefault((row["case_id"], row["model"]), {})[row["settings"]["quality"]] = row
    pairs = []
    for (case, model), qualities in groups.items():
        if "low" in qualities and "medium" in qualities:
            low, medium = qualities["low"], qualities["medium"]
            if low["prompt_sha256"] != medium["prompt_sha256"] or low["reference_assets"] != medium["reference_assets"]:
                raise ValueError("Human quality pair must use identical prompt and references")
            pairs.append((case, model, low, medium))
    columns = ["case_id", "model_requested", "low_run_id", "medium_run_id", "prompt_sha256",
               "asset_basis", "reviewer", "preferred_quality", "instruction_adherence",
               "panel_step_correspondence", "geometry_consistency", "legibility",
               "reference_fidelity", "notes", "reviewed_at_utc"]
    with (root / "human-comparison.csv").open("w", newline="", encoding="utf-8") as stream:
        writer = csv.DictWriter(stream, fieldnames=columns)
        writer.writeheader()
        for case, model, low, medium in pairs:
            writer.writerow({"case_id": case, "model_requested": model, "low_run_id": low["run_id"],
                             "medium_run_id": medium["run_id"], "prompt_sha256": low["prompt_sha256"],
                             "asset_basis": "synthetic"})
    parts = ['<!doctype html><html lang="en"><meta charset="utf-8"><title>HowLens human image comparison</title>',
             '<style>body{font:16px system-ui;margin:2rem;max-width:1200px}section{margin:2rem 0}.pair{display:flex;gap:1rem;flex-wrap:wrap}figure{flex:1;margin:0;min-width:260px}img{width:100%}table{border-collapse:collapse}td,th{border:1px solid #aaa;padding:.4rem}code{overflow-wrap:anywhere}</style>',
             '<h1>Generated guide output: human review pending</h1>',
             '<p>All fixtures are synthetic sandbox material. This sheet does not evaluate input photo recognition, equipment safety, or production guide approval. Source manual figures must be preserved through deterministic composition; supplementary generation is only for missing figures.</p>',
             '<p>Review raw images for instruction adherence, panel to step correspondence, invented geometry, and legibility. Fill the CSV manually; blank means unscored. Reference fidelity cannot be evaluated here because the fixtures have no reference image. Do not infer a winner or p95 from three cases.</p>']
    def card(row):
        detail = artifacts["runs"][row["run_id"]]
        raw_path = detail.get("raw_path")
        picture = '<p>No output: ' + html.escape(row["status"]) + '</p>'
        if raw_path:
            relative = Path(raw_path).resolve().relative_to(root).as_posix()
            picture = '<img alt="Synthetic sandbox output" src="' + html.escape(relative, quote=True) + '">'
        return '<figure><figcaption>' + html.escape(row["model"] + ' / ' + row["settings"]["quality"]) + '</figcaption>' + picture + '</figure>'
    for case, model, low, medium in pairs:
        parts.append('<section><h2>' + html.escape(case + ' / ' + model) + '</h2><div class="pair">' + card(low) + card(medium) + '</div></section>')
    if not pairs:
        parts.append('<p>This configuration has no low/medium pairs.</p>')
    parts.append('<h2>All planned attempts</h2><table><tr><th>Case</th><th>Model / quality</th><th>Status</th><th>Latency ms</th><th>Usage estimate USD</th><th>Receipt USD</th></tr>')
    for row in rows:
        detail = artifacts["runs"][row["run_id"]]
        cells = [row["case_id"], row["model"] + ' / ' + row["settings"]["quality"], row["status"],
                 row["latency_ms"], detail["cost"]["estimated_total_cost_usd"], detail["cost"]["actual_receipt_cost_usd"]]
        parts.append('<tr>' + ''.join('<td>' + html.escape(str(v) if v is not None else 'unmeasured') + '</td>' for v in cells) + '</tr>')
    parts.append('</table><p>Cost estimates use list rates and are not billing receipts. Review fields remain blank until a human records them.</p></html>')
    (root / "human-comparison.html").write_text('\n'.join(parts), encoding="utf-8")
