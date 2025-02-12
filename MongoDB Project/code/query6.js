// Query 6
// Find the average friend count per user.
// Return a decimal value as the average user friend count of all users in the users collection.

function find_average_friendcount(dbname) {
    db = db.getSiblingDB(dbname);

    // TODO: calculate the average friend count
    let totalFriendCount = 0;
    let totalUserCount = 0;
    db.users.aggregate([
        {$project:{friendCount:{$size:"$friends"}}}  
    ]).forEach(user=>{
        totalFriendCount+=user.friendCount;
        totalUserCount++;
    });
    const averageFriendCount = totalFriendCount/totalUserCount;
    return averageFriendCount;
}
