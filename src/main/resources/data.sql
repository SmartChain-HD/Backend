INSERT INTO role (name, code) VALUES ('게스트', 'GUEST') ON CONFLICT (code) DO NOTHING;
INSERT INTO role (name, code) VALUES ('기안자', 'DRAFTER') ON CONFLICT (code) DO NOTHING;
INSERT INTO role (name, code) VALUES ('결재자', 'APPROVER') ON CONFLICT (code) DO NOTHING;
INSERT INTO role (name, code) VALUES ('심사자', 'REVIEWER') ON CONFLICT (code) DO NOTHING;
