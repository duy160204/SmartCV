async function run() {
    try {
        console.log("1. Logging in...");
        const res = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'duylv0204@gmail.com', password: '123' })
        });
        
        if (!res.ok) {
            console.log("Login failed with status:", res.status);
            const text = await res.text();
            console.log("Response:", text);
            
            // Try with 123456
            console.log("\nRetrying login with 123456...");
            const res2 = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: 'duylv0204@gmail.com', password: '123456' })
            });
            if (!res2.ok) {
                console.log("Login 2 failed:", res2.status, await res2.text());
                return;
            }
            return await processToken(await res2.json());
        }
        
        await processToken(await res.json());

    } catch (e) {
        console.error(e);
    }
}

async function processToken(data) {
    const token = data.accessToken;
    console.log("Login success! Role:", data.role);
    console.log("Token:", token.substring(0, 20) + "...");
    
    // Decode token manually
    const parts = token.split('.');
    const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString('utf8'));
    console.log("Decoded Token Payload:", payload);

    console.log("\n2. Accessing /api/users/me...");
    const meRes = await fetch('http://localhost:8080/api/users/me', {
        headers: { Authorization: 'Bearer ' + token }
    });
    console.log("GET /me status:", meRes.status);
    console.log("GET /me response:", await meRes.text());

    console.log("\n3. Accessing /api/admin/users...");
    const adminRes = await fetch('http://localhost:8080/api/admin/users', {
        headers: { Authorization: 'Bearer ' + token }
    });
    console.log("GET /admin/users status:", adminRes.status);
    console.log("GET /admin/users response:", await adminRes.text());
}

run();
