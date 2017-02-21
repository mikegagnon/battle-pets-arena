# pip install requests

import os
import requests

CONTEST_SERVICE_API_TOKEN = os.environ['CONTEST_SERVICE_API_TOKEN']

# r = requests.get('https://api.github.com/user', auth=('user', 'pass'))

r = requests.post('http://localhost:9000/contest',
        headers = {
            'Contest-Token': CONTEST_SERVICE_API_TOKEN,
            'Content-Type': 'application/json'},
        json = {
            "petId1": "2251ef5c-4abb-4f97-943e-0dc8738b5844",
            "petId2": "1d4d557b-2470-40cb-b2e4-1bc138914464",
            "contestType": "muscle"})

print r