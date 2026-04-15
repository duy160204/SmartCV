const crypto = require('crypto');
function hmacSHA512(key, data) {
    return crypto.createHmac('sha512', key).update(data, 'utf8').digest('hex');
}
const secret = "COGQB1HR";
const data_plus = "vnp_OrderInfo=Thanh+toan";
const data_20 = "vnp_OrderInfo=Thanh%20toan";
console.log("Plus :", hmacSHA512(secret, data_plus));
console.log("20   :", hmacSHA512(secret, data_20));
