import copy
import json
from pathlib import Path
import tempfile
import unittest
from evaluation.image_models import protocol as p


class ProtocolTests(unittest.TestCase):
    def setUp(self):
        self.case=p.cases(p.ROOT/'cases.jsonl')[0]
        self.row=p.plan([self.case],'test-model',{'quality':'low'})[0]

    def test_plan_no_measurement_or_approval(self):
        self.assertIsNone(self.row['latency_ms'])
        self.assertFalse(self.row['product_safety_approval'])
        self.assertEqual(p.summarize([self.row])['live_groups'],{})
        self.assertEqual(self.row,p.plan([self.case],'test-model',{'quality':'low'})[0])

    def test_reject_dry_run_latency(self):
        self.row['latency_ms']=1
        with self.assertRaises(ValueError):p.summarize([self.row])

    def test_exclude_offline_measurement(self):
        self.row.update(status='success',measurement_source='offline',latency_ms=3)
        self.assertEqual(p.summarize([self.row])['live_groups'],{})

    def test_geometry_and_order(self):
        panels=[{'index':i,'step_id':s,'box':[i%3*32,i//3*32,(i%3+1)*32,(i//3+1)*32]} for i,s in enumerate(self.case['scene_step_ids'])]
        self.assertEqual(p.geometry(96,96,panels,self.case['scene_step_ids'])['scene_semantics'],'requires_human_review')
        for w,h,ps in [(97,96,panels),(96,96,panels[::-1]),(96,96,panels[:8])]:
            with self.assertRaises(ValueError):p.geometry(w,h,ps,self.case['scene_step_ids'])
        panels[0]['step_id']='invented'
        with self.assertRaises(ValueError):p.geometry(96,96,panels,self.case['scene_step_ids'])

    def test_cost_proxy_and_tokens_not_double_counted(self):
        rate=json.loads((p.ROOT/'gpt-image-1.5.rate.json').read_text())
        self.assertEqual(p.estimate(rate,{'text_input':500,'images':1}),.0115)
        self.assertEqual(p.estimate(rate,{'text_input':500,'image_output':1000,'images':9}),.0345)
        with self.assertRaises(ValueError):p.estimate(rate,{'text_input':-1})
        with self.assertRaises(ValueError):p.estimate({'image_output':30},{'images':1})

    def test_latency_threshold_and_unknown_cost(self):
        rows=[]
        for i in range(20):
            row=copy.deepcopy(self.row)
            row.update(status='success',measurement_source='live',run_id=str(i),latency_ms=i+1)
            rows.append(row)
        group=next(iter(p.summarize(rows[:3])['live_groups'].values()))
        self.assertIsNone(group['p95_ms'])
        self.assertIsNone(group['actual_total_cost_usd'])
        group=next(iter(p.summarize(rows)['live_groups'].values()))
        self.assertEqual((group['p50_ms'],group['p95_ms']),(10,19))

    def test_retry_error_and_config_separation(self):
        a=copy.deepcopy(self.row);a.update(status='api_error',measurement_source='live',actual_cost_usd=.01)
        b=copy.deepcopy(a);b.update(attempt=2,status='success',latency_ms=20)
        group=next(iter(p.summarize([a,b])['live_groups'].values()))
        self.assertEqual(group['api_error_rate'],.5)
        self.assertEqual(group['retry_run_rate'],1)
        self.assertEqual(group['actual_total_cost_usd'],.02)
        with self.assertRaises(ValueError):p.summarize([a,a])
        b['settings']={'quality':'high'}
        self.assertEqual(len(p.summarize([a,b])['live_groups']),2)

    def test_scoring_requires_humans_and_preserves_text(self):
        from PIL import Image
        with tempfile.TemporaryDirectory() as tmp:
            path=Path(tmp)/'TEST.png';Image.new('RGB',(96,96),'white').save(path)
            r=copy.deepcopy(self.row);r.update(status='success',measurement_source='offline',panels=[{'index':i,'step_id':s,'box':[i%3*32,i//3*32,(i%3+1)*32,(i//3+1)*32]} for i,s in enumerate(self.case['scene_step_ids'])])
            self.assertEqual(p.score(self.case,r,path)['verdict'],'pending_human_review')
            r['human_review']={'reviewer':'TEST ONLY','reviewed_at_utc':'TEST ONLY','ratings':dict.fromkeys(p.CRITERIA,2)}
            self.assertEqual(p.score(self.case,r,path)['verdict'],'candidate_pass')
            r['human_review']['ratings']['no_invented_action']=0
            self.assertEqual(p.score(self.case,r,path)['verdict'],'reject_unsafe')
            r['text_sha256']='changed'
            self.assertEqual(p.score(self.case,r,path)['verdict'],'reject_text_changed')
