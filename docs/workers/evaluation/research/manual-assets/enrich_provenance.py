"""Attach reviewed source metadata to inventory, without product catalog registration."""
import json
from pathlib import Path
root=Path(__file__).parent
m=json.loads((root/'INVENTORY.json').read_text())
rows=[
('poweredge-r750_Owners-Manual_en-us.pdf','Dell PowerEdge R750 Installation and Service Manual','December 2024 Rev A11','https://dl.dell.com/topicspdf/poweredge-r750_Owners-Manual_en-us.pdf',[{'pdf_page':1,'printed_page':None,'purpose':'version'},{'pdf_page':211,'printed_page':'211','purpose':'Figure 212 specimen'}]),
('710-965-00_UR5e_User_Manual_en_Global.pdf','UR5e User Manual','PolyScope5.23; Document10.13.387;710-965-00','https://s3-eu-west-1.amazonaws.com/ur-support-site/260652/710-965-00_UR5e_User_Manual_en_Global.pdf',[{'pdf_page':244,'printed_page':None,'purpose':'version'},{'pdf_page':194,'printed_page':'197','purpose':'stop section, no approval'}]),
('APC_SmartUPS_Tower_OperationManual_990-3534H_EN.pdf','Smart-UPS Tower Operation Manual','EN990-3534H;03/2023','https://download.schneider-electric.com/files?p_Doc_Ref=SPD_SCON-7NBSEM_EN',[{'pdf_page':20,'printed_page':None,'purpose':'version'}]),
('APC_SmartUPS_Tower_InstallationGuide_990-3535H_EN.pdf','Smart-UPS Tower Installation Guide','EN990-3535H-001;05/2022','https://download.schneider-electric.com/files?p_Doc_Ref=SPD_SCON-7QHM74_EN',[{'pdf_page':8,'printed_page':None,'purpose':'version'}]),
('APC_RBC_Battery_Cartridge_Replacement_InstructionSheet_990-0179L_EN.pdf','Battery Cartridge Replacement','990-0179L;9/2012','https://download.schneider-electric.com/files?p_enDocType=Instruction+sheet&p_File_Name=EALN-7N3P8W_R1_EN.pdf&p_Doc_Ref=SPD_EALN-7N3P8W_EN',[{'pdf_page':8,'printed_page':None,'purpose':'version'}]),
('APC_RBC_Addendum_PostInstall_ReplaceBatteryLED_990-0374A_EN.pdf','Replacement Battery Cartridge Addendum','990-0374A revision2;11/01','https://download.schneider-electric.com/files?p_enDocType=User+guide&p_File_Name=ASTE-6Z8LBP_R0_EN.pdf&p_Doc_Ref=SPD_ASTE-6Z8LBP_EN',[{'pdf_page':1,'printed_page':None,'purpose':'version'}])]
out=[]
for filename,title,version,url,pages in rows:
 x=next(x for x in m['files'] if Path(x['path']).name==filename)
 out.append(dict(path=x['path'],archive_path=x['archive_path'],sha256=x['sha256'],bytes=x['bytes'],pdf_page_count=x['pages'],title=title,document_version=version,source_url=url,source_url_provenance='prework MANUALS.md; official URL, web-open result in SOURCES.md',page_identities=pages,rights_status='restricted/no redistribution license established',review_status='source identity only; actions not approved',field_device_match='pending',verified_date='2026-10-09'))
(root/'MANUAL_PROVENANCE.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n')
