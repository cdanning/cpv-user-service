# User Role Managment

__Logs will be sent to http://localhost:8000/log__ see `conf/application.conf`

## REST Endpoints

### get a list of all users as json
`curl -X GET localhost:9000/users -v`

### create a new user
`curl -X POST localhost:9000/users --data-binary '{ "pseudonym": "anton", "roles": [ "admin", "read" ] }' -H "Content-Type: application/json" -v` 

### grant a role to a user
`curl -X POST localhost:9000/users/user/anton/role/write -v`

### revoke a role from a user
`curl -X DELETE localhost:9000/users/user/anton/role/read -v`

### delete a user
`curl -X DELETE localhost:9000/users/user/anton -v`

