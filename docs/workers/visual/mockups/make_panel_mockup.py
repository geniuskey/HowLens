"""Quick synthetic presentation preview; run with visual venv and backend PYTHONPATH."""
from datetime import datetime
from pathlib import Path

from PIL import Image, ImageDraw
from visual.service import scene_step_ids

stamp = datetime.now().strftime("%Y%m%d_%H%M")
title = stamp + "_panel_step_source_labels"
fixture = {"decision": "guide", "mode": "live", "preconditions": [],
           "evidence": [{"evidence_id": "SYNTHETIC-E1"}],
           "steps": [{"step_id": "SYNTHETIC-S" + str(i),
                      "description": "Synthetic approved action " + str(i),
                      "evidence_ids": ["SYNTHETIC-E1"]} for i in range(1, 4)]}
ids = scene_step_ids(fixture)
image = Image.new("RGB", (940, 760), "#F4F5F7")
draw = ImageDraw.Draw(image)
draw.text((20, 16), title, fill="#202630")
draw.text((20, 42), "SYNTHETIC UI MOCKUP - no equipment photo or real generation", fill="#B13B37")
draw.text((20, 64), "Labels come from stored steps/evidence; geometry is not semantic approval.", fill="#465364")
colors = ["#A4C8DE", "#B8D6C0", "#E6D0A5"]
for index, step_id in enumerate(ids):
    row, col = divmod(index, 3)
    x, y = 20 + col * 305, 105 + row * 210
    draw.rectangle((x, y, x + 290, y + 194), fill="white", outline="#D4D8DE")
    draw.rectangle((x + 10, y + 10, x + 280, y + 111), fill=colors[index // 3])
    draw.text((x + 22, y + 35), "ILLUSTRATION PLACEHOLDER", fill="#354657")
    draw.text((x + 12, y + 123), "Panel " + str(index) + " | " + step_id, fill="#202630")
    draw.text((x + 12, y + 145), "Source: SYNTHETIC-E1", fill="#465364")
    draw.text((x + 12, y + 166), "Demo manual v0 / PDF p1", fill="#465364")
draw.text((20, 742), "Repeated panels represent the same stored action; no new procedures are created.", fill="#465364")
path = Path(__file__).parent / (title + ".png")
image.save(path)
print(path.resolve())
