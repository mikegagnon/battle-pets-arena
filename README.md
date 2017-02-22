
# Battle Pets Arena API

This API is used to run contests for pets.
Pets comes from from the [Pet API](https://github.com/wunderteam/battle-pets-api).

## Endpoints:

All endpoints require an API key to access.

This key is passed via the `Contest-Token` HTTP header.

## Create contest

`POST /contest`

JSON payload:

```
{
  "petId1": "...",
  "petId2": "...",
  "contestType": "muscle"
}
```

### Available contests types

- "muscle"
- "wits"
- "slow"
- "very-slow"

### Response

Returns a JSON string containing the ID for the contest, with status 201 created.

## Request contest result

`GET /contest/result`

### Possible responses

`400 Bad Request "Invalid contestId"`

Or:

`200 OK` with JSON response:

```
{
  "code": integer,
  "message: string
  "result": {
    "firstPlace": petName string
    "secondPlace": petName string
    "summary": string
  }
}
```

`result` is optional and is only included if the contest has completed.
Within `result`, `summary` is optional, and summaries the most exciting event(s) in the contest.

If `code` is positive, that indicates a non-error.

If `code` is negative, that indicates an error.

### Enumeration of possible responses

```
{
  "code": 1
  "message": "Contest in progress"
}
```

```
{
  "code": 2,
  "message: "Contest complete"
  "result": {
    "firstPlace": petName string
    "secondPlace": petName string
    "summary": string
  }
}
```

```
{
  "code": -1
  "message": "Could not find pet"
}
```

```
{
  "code": -2
  "message": "Internal server error"
}
```

```
{
  "code": -3
  "message": "Error accessing Pet service at " + petApiHost
}
```

```
{
  "code": -4
  "message": "Could not parse json from Pet service"
}
```

```
{
  "code": -5
  "message": "Error: you specified an invalid contest. Available contests: " ...
}
```

```
{
  "code": -6
  "message": "Error: the contestId does not match any contests"
}
```

## Environment variable

`CONTEST_SERVICE_API_TOKEN`

## Run server

```
sbt run
```

## Run tests

```
python test.py
```

## Areas for improvement

- Use a real database. As is, the database is kept in memory in a `Map` object. Consequently, all contests are lost whenever the server shuts down. On the upside, it would be relatively easy to add a database by simply modifying the implementation of the DatabaseActor
- Scala tests with mock Pet API
- Return better response status codes for errors 
