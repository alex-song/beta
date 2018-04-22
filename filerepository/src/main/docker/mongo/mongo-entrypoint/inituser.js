// MongoDB initialization script for file repository project
// Create super user in admin database
var db = connect("admin");
db.createUser(
    {
        user:"alexsong",
        pwd:"Asd123",
        roles:[{role: "root", db: "admin"}]
    }
);

// Create frs-grid database, that persists large files using grid fs
// db = db.getSiblingDB('frs-grid');
// Create frs database
db = db.getSiblingDB('frs');

// Create app user, and grant dbOwner role in both frs and frs-grid databases
db.createUser(
    {
        user:"frs",
        pwd:"frs",
        roles:[{role:"dbOwner", db:"frs"}]
    }
);


