const express = require('express');
const bodyParser = require('body-parser');

const app = express();
app.use(bodyParser.json());

// In-memory set to deduplicate SOS messages by messageId
const seenMessages = new Set();

app.post('/relay-sos', (req, res) => {
    const { messageId, destinationPhoneNumber, text } = req.body;

    if (!messageId || !destinationPhoneNumber || !text) {
        return res.status(400).json({ error: 'Missing required fields' });
    }

    if (seenMessages.has(messageId)) {
        console.log(`[SOS GATEWAY] Duplicate SOS received: ${messageId}. Ignoring.`);
        return res.status(200).json({ status: 'duplicate' });
    }

    seenMessages.add(messageId);

    console.log('\n=============================================');
    console.log('🚨 EMERGENCY SOS RECEIVED 🚨');
    console.log('=============================================');
    console.log(`Message ID: ${messageId}`);
    console.log(`To:         ${destinationPhoneNumber}`);
    console.log(`Message:    ${text}`);
    console.log('=============================================');
    console.log('Mock: Sent via SMS API.\n');

    // In a real implementation, you would call an SMS API (like Twilio) here.
    // Example:
    // client.messages.create({
    //     body: text,
    //     from: '+12345678901',
    //     to: destinationPhoneNumber
    // }).then(message => console.log(message.sid));

    return res.status(200).json({ status: 'delivered' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`SOS Gateway running on port ${PORT}`);
});
