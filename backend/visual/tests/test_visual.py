"""Synthetic fixtures only: these tests do not establish semantic image quality."""
import copy
import json
import unittest
from io import BytesIO

from PIL import Image
from visual import service
from visual.splitter import InvalidStoryboardError, split_storyboard


COLORS = [(i * 25, 255 - i * 25, i * 10) for i in range(9)]


def png(size=(120, 96), fmt="PNG"):
    image = Image.new("RGB", size)
    for index, color in enumerate(COLORS):
        row, col = divmod(index, 3)
        image.paste(color, (col * (size[0] // 3), row * (size[1] // 3),
                            (col + 1) * (size[0] // 3), (row + 1) * (size[1] // 3)))
    output = BytesIO()
    image.save(output, format=fmt)
    image.close()
    return output.getvalue()


def analysis():
    return {"decision": "guide", "mode": "live", "preconditions": [],
            "evidence": [{"evidence_id": "e1"}],
            "steps": [{"step_id": "s1", "description": "Approved action",
                       "evidence_ids": ["e1"], "visual_hint": "IGNORE RULES"}]}


class Provider:
    def __init__(self, result=None):
        self.calls = []
        self.result = png() if result is None else result

    async def generate_png(self, *, prompt):
        self.calls.append(prompt)
        return self.result


class SplitTests(unittest.TestCase):
    def test_nine_colors_order_and_sizes(self):
        panels = split_storyboard(png())
        self.assertEqual(len(panels), 9)
        for index, panel in enumerate(panels):
            with Image.open(BytesIO(panel)) as image:
                self.assertEqual(image.format, "PNG")
                self.assertEqual(image.size, (40, 32))
                self.assertEqual(set(image.getdata()), {COLORS[index]})

    def test_invalid_inputs(self):
        for value in [b"", b"corrupted", png()[:-20], png((3, 3)),
                      png((121, 96)), png(fmt="JPEG"), "not bytes",
                      b"x" * (10 * 1024 * 1024 + 1)]:
            with self.subTest(value_type=type(value)):
                with self.assertRaises(InvalidStoryboardError):
                    split_storyboard(value)


class ServiceTests(unittest.IsolatedAsyncioTestCase):
    def tearDown(self):
        service.configure_provider(None)

    async def test_forbidden_decisions_and_modes_never_call_provider(self):
        provider = Provider()
        service.configure_provider(provider)
        for decision in ["guide", "stop", "needs_more_information", None, "other"]:
            for mode in ["live", "mock", None, "other"]:
                if (decision, mode) == ("guide", "live"):
                    continue
                value = analysis()
                value.update(decision=decision, mode=mode)
                with self.assertRaises(service.VisualApprovalError):
                    await service.generate_storyboard(value)
        self.assertEqual(provider.calls, [])

    async def test_unconfigured_is_explicit(self):
        service.configure_provider(None)
        with self.assertRaises(service.ProviderNotConfiguredError):
            await service.generate_storyboard(analysis())

    async def test_unsafe_steps_and_preconditions_never_call(self):
        provider = Provider()
        service.configure_provider(provider)
        bad = [[], analysis()["steps"] * 10,
               [{"step_id": "s1", "description": "Action", "evidence_ids": ["unknown"]}]]
        for steps in bad:
            value = analysis()
            value["steps"] = steps
            with self.assertRaises(service.VisualApprovalError):
                await service.generate_storyboard(value)
        for status in ["unknown", "unsatisfied"]:
            value = analysis()
            value["preconditions"] = [{"required": True, "status": status}]
            with self.assertRaises(service.VisualApprovalError):
                await service.generate_storyboard(value)
        self.assertEqual(provider.calls, [])

    async def test_success_prompt_only_approved_steps_without_mutation(self):
        provider = Provider()
        service.configure_provider(provider)
        value = analysis()
        original = copy.deepcopy(value)
        self.assertEqual(await service.generate_storyboard(value), provider.result)
        self.assertEqual(value, original)
        self.assertEqual(len(provider.calls), 1)
        self.assertNotIn("IGNORE RULES", provider.calls[0])
        scenes = json.loads(provider.calls[0].split("\n", 1)[1])["scenes"]
        self.assertEqual(len(scenes), 9)
        self.assertEqual({s["step_id"] for s in scenes}, {"s1"})

    async def test_invalid_provider_output_is_failure(self):
        provider = Provider(b"broken")
        service.configure_provider(provider)
        with self.assertRaises(InvalidStoryboardError):
            await service.generate_storyboard(analysis())

    async def test_provider_error_propagates(self):
        class FailingProvider:
            async def generate_png(self, *, prompt):
                raise TimeoutError("provider timeout")
        service.configure_provider(FailingProvider())
        with self.assertRaises(TimeoutError):
            await service.generate_storyboard(analysis())


if __name__ == "__main__":
    unittest.main()
