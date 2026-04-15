#!/bin/bash

# ==========================================
# TEST VNPay IPN - Local Bypass Signature
# ==========================================

# NOTE: Thay <TXN_REF> bằng mã transaction UUID sinh ra khi gọi POST /api/payments
# Thay <AMOUNT> bằng đúng số tiền trong DB (vd: gói 99000 -> lấy 9900000)

TXN_REF="20601934-2ee6-4e5a-93f4-754d9c49ca18"
AMOUNT="9900000"

BASE_URL="http://localhost:8080/api/payments/vnpay/ipn"

# Cấu trúc query - Note: Bypass signature thì vnp_SecureHash điền gì cũng được
QUERY="vnp_TxnRef=${TXN_REF}&vnp_ResponseCode=00&vnp_Amount=${AMOUNT}&vnp_SecureHash=dummyhash"

URL="${BASE_URL}?${QUERY}"

echo "==================================="
echo "1. Single Request Test (Expected: SUCCESS)"
echo "==================================="
curl -X GET "$URL"
echo -e "\n\n"

echo "==================================="
echo "2. Race Condition Simulation: 10 parallel requests (Expected: only 1 Success, others ignored/bounced by idempotency)"
echo "==================================="
for i in {1..10}
do
   curl -X GET "$URL" &
done
wait
echo -e "\n\nRace Condition Test Complete!\n"
