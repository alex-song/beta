var db = connect("mongodb://localhost/admin");

db.createUser(
    {
        user: "alexsong",
        pwd: "Asd123",
        roles:['root']
    }
)