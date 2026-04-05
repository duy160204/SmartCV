const axios = require('axios');
const http = require('http');

const server = http.createServer((req, res) => {
    console.log('Received Headers:', req.headers);
    res.writeHead(200);
    res.end('OK');
});

server.listen(3002, async () => {
    const api = axios.create({ baseURL: 'http://localhost:3002' });
    api.interceptors.request.use(config => {
        const fakeToken = "MY_FAKE_TOKEN";
        config.headers.Authorization = `Bearer ${fakeToken}`;
        return config;
    });
    try {
        await api.get('/');
    } catch(e) { console.log(e); }
    server.close();
});
