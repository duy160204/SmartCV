import express from "express";
import puppeteer from "puppeteer";

const app = express();
app.use(express.json({ limit: "10mb" }));

app.post("/render-pdf", async (req, res) => {
  const { html } = req.body;

  try {
    const browser = await puppeteer.launch({
      args: ["--no-sandbox", "--disable-setuid-sandbox"]
    });

    const page = await browser.newPage();

    page.on("request", req => {
      if (req.resourceType() === "image") {
        console.log("➡️ IMAGE REQUEST:", req.url());
      }
    });

    page.on("response", res => {
      if (res.request().resourceType() === "image") {
        console.log("📦 IMAGE RESPONSE:", res.status(), res.url());
      }
    });

    page.on("requestfailed", req => {
      if (req.resourceType() === "image") {
        console.log("❌ IMAGE FAILED:", req.url(), req.failure());
      }
    });

    let finalHtml = html;
    if (finalHtml && finalHtml.includes("<head>")) {
      finalHtml = finalHtml.replace("<head>", '<head><base href="http://localhost:8080/" />');
    } else if (finalHtml) {
      finalHtml = '<base href="http://localhost:8080/" />' + finalHtml;
    }
    
    console.log("========== FINAL HTML START ==========");
    console.log(finalHtml);
    console.log("========== FINAL HTML END ==========");

    await page.setContent(finalHtml, {
      waitUntil: "networkidle0",
      url: "http://localhost:8080"
    });

    // Wait for all images to naturally load
    await page.evaluate(() => {
      return Promise.all(
        Array.from(document.images).map(img => {
          if (img.complete) return;
          return new Promise(resolve => {
            img.onload = img.onerror = resolve;
          });
        })
      );
    });

    const pdf = await page.pdf({
      format: "A4",
      printBackground: true,
      margin: { top: 0, bottom: 0, left: 0, right: 0 }
    });

    await browser.close();

    res.set({
      "Content-Type": "application/pdf"
    });
    res.send(pdf);

  } catch (err) {
    console.error(err);
    res.status(500).send("PDF render failed");
  }
});

app.listen(3002, () => console.log("PDF service running"));
