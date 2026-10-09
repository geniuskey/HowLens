"""Small offline reviewer sheet; never renders a generated/live equipment claim."""
from datetime import datetime
from pathlib import Path
from PIL import Image, ImageDraw

title = datetime.now().strftime("%Y%m%d_%H%M") + "_image_model_reviewer_sheet"
image = Image.new("RGB", (1100, 940), "#F4F5F7")
draw = ImageDraw.Draw(image)
draw.text((24, 20), title, fill="#202630")
draw.text((24, 48), "SYNTHETIC PLACEHOLDERS ONLY - NO GENERATED/LIVE OUTPUT - PAID CALLS 0", fill="#A63333")
draw.text((24, 75), "Cost: unknown / billing null | Speed: not measured | No winner or quality score yet", fill="#465364")
models = ["gpt-image-1.5", "gpt-image-1-mini"]
cases = [("TEST-server", "2 indicators / centered display / 3 vents"),
         ("TEST-cobot", "2 links / 3 joints / arrow+OBSERVE review"),
         ("TEST-ups", "1 indicator / display / 2 vents")]
for col, model in enumerate(models):
    x = 24 + col * 535
    draw.rectangle((x, 110, x + 510, 165), fill="#253B50")
    draw.text((x + 14, 122), model + " | 1024 square / low", fill="white")
    draw.text((x + 14, 144), "3 identical prompts | sequential | retry 0", fill="white")
    for row, (case, topology) in enumerate(cases):
        y = 180 + row * 220
        draw.rectangle((x, y, x + 510, y + 205), fill="white", outline="#CFD5DC")
        draw.text((x + 12, y + 12), case + " | source: Evaluation synthetic case", fill="#202630")
        for i in range(9):
            gx, gy = x + 12 + (i % 3) * 38, y + 38 + (i // 3) * 38
            draw.rectangle((gx, gy, gx + 33, gy + 33), fill=["#ABC8DD", "#BFD4C6", "#E1D1AE"][i // 3])
            draw.text((gx + 9, gy + 9), str(i), fill="#354657")
        draw.text((x + 143, y + 44), "9 cells: placeholder only", fill="#465364")
        draw.text((x + 143, y + 70), "Latency: NOT RUN | actual USD: null", fill="#465364")
        draw.text((x + 143, y + 96), "Topology target: " + topology, fill="#465364")
        draw.text((x + 143, y + 122), "Scene match / visible grid: pending", fill="#465364")
        draw.text((x + 143, y + 148), "Arrows / labels / no new action: pending", fill="#465364")
        draw.text((x + 12, y + 184), "Reviewer A: ____   Reviewer B: ____   Adjudication: ____", fill="#465364")
draw.text((24, 865), "After authorized generation: blind reviewer copies; raw/normalized hashes and failures retained.", fill="#465364")
draw.text((24, 892), "Grid splitting proves container geometry only. Semantic/topology acceptance requires human review.", fill="#A63333")
path = Path(__file__).parent / (title + ".png")
image.save(path)
print(path.resolve())
