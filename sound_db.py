import sqlite3
import os, re

def under2Pascal(name):
    return re.sub('(_)([a-z])', lambda g:
                  ' ' + g.group(2).upper(), name.capitalize())
base_path = r'D:\Documents\sounds'
if not os.path.exists(base_path):
    exit(1)

conn = sqlite3.connect(r'D:\Documents\unit.db')
c = conn.cursor()
c.execute('''CREATE TABLE unit_db
    (id INTEGER PRIMARY KEY NOT NULL, name TEXT, race TEXT, sounds TEXT,
    f_image BLOB, s_image BLOB)''')

for race in os.listdir(base_path):
    race_name = under2Pascal(race)
    
    for unit_name in os.listdir(os.path.join(base_path, race)):
        file_path = os.path.join(base_path, race, unit_name)
        files = os.listdir(file_path)

        sounds = '|'.join([s[:-4] for s in files if '.mp3' in s])
        name = under2Pascal(unit_name)
        face_image = ''.join([i for i in files if 'unit.png' in i])
        face_image = os.path.join(file_path, face_image)
        f_image = open(face_image, "rb").read()
        special_image = ''.join([i for i in files if 'special.png' in i])
        special_image = os.path.join(file_path, special_image)
        s_image = open(special_image, "rb").read()

        c.execute('''INSERT INTO unit_db (name, race, sounds, f_image, s_image)
    VALUES (?,?,?,?,?)''',
        [name, race_name, sounds, sqlite3.Binary(f_image),
        sqlite3.Binary(s_image)])


for entry in c.execute('''SELECT id, race, name, sounds FROM unit_db LIMIT 5'''):
    print(entry)
conn.commit()
conn.close()
       
##conn = sqlite3.connect('./result.db')
##c = conn.cursor()
##
##c.execute('''CREATE TABLE unit_database
##        (name text, race text, soundlist text, unit_image blob, special_image blob)''')
##
##c.execute('''INSERT INTO unit_database VALUES
##    ('Footman', 'Human', 'F1.mp3|F2.mp3|F3.mp3', 1212121, 123123123)''')
##
##for entry in c.execute('''SELECT * FROM unit_database LIMIT 3'''):
##    print(entry)
##    
##conn.commit()
##conn.close()
