# Interview Assignment
###Rest Api Documentation
* Login (POST):
    * Returns a hash for authentication and the current inventory
    * Json: {"username":String, "password":String}
    * Expected Output: 200 OK {"data":[{"name":String, "amount":Int, "id":Int}], "auth":String}
    
* Register (POST):
    * Registers a new user
    * Json: {"username":String, "password":String}
    * Expected Output: 200 OK {"auth":String}
    
* Add to inventory (POST):
    * Adds a new item to the inventory, returns id to access new inventory item
    * Json: {"username":String, "name": String, "amount":Int}
    * Expected Output: 200 OK {"id":Int}
    
* Update inventory (PUT):
    * Updates an existing item in the inventory
    * Json: {"username":String, "data":[{"id":Int, "name":String, "amount":Int}]}
    * Expected Output: 204 No Content
    
* Delete from inventory (DELETE):
    * Deleted an existing item from inventory
    * Json: {"id":Int}
    * Expected Output: 204 No Content