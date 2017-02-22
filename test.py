# pip install requests

import os
import requests
from time import sleep

CONTEST_SERVICE_API_TOKEN = os.environ['CONTEST_SERVICE_API_TOKEN']

def createContest(
        contestType,
        petId1 = "2251ef5c-4abb-4f97-943e-0dc8738b5844",
        petId2 = "1d4d557b-2470-40cb-b2e4-1bc138914464"):
    r = requests.post('http://localhost:9000/contest',
            headers = {
                'Contest-Token': CONTEST_SERVICE_API_TOKEN,
                'Content-Type': 'application/json'},
            json = {
                "petId1": petId1,
                "petId2": petId2,
                "contestType": contestType})
    return r.json()

def getResult(contestId):
    r = requests.get('http://localhost:9000/contest/result/' + contestId,
            headers = {'Contest-Token': CONTEST_SERVICE_API_TOKEN})
    return r.json()

def waitForResult(contestId):
    result = getResult(contestId)
    while result["code"] == 1 or result["code"] == -6:
        print "Waiting for contest " + contestId + " to complete"
        sleep(1)
        result = getResult(contestId)
    return result


# A successful fast contest
contestId = createContest("muscle")
result = waitForResult(contestId)
assert result["code"] == 2
assert result["result"]["firstPlace"] == "Fluffy"
assert result["result"]["secondPlace"] == "Max"

# A successful slow contest
contestId = createContest("slow")
result = waitForResult(contestId)
assert result["code"] == 2
assert result["result"]["firstPlace"] == "Fluffy"
assert result["result"]["secondPlace"] == "Max"

# Bad pet ID
contestId = createContest("muscle", petId1="badid")
result = waitForResult(contestId)
assert result["code"] == -3

# Bad game
contestId = createContest("badgame")
result = waitForResult(contestId)
assert result["code"] == -5

# Malformed contest id
result = getResult("Malformed")
assert result == "Invalid contestId"

# Bad contest id
result = getResult("caf8c135-91c8-44ae-a34b-f8a612de547f")
assert result["code"] == -6

# Missing security token
r = requests.get('http://localhost:9000/contest/result/foo')
assert r.status_code == 401
