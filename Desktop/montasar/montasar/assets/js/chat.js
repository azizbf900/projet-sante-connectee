document.addEventListener('DOMContentLoaded', function() {
    const messageIcon = document.getElementById('message');
    const chatDropdown = document.getElementById('chat-dropdown');
    const closeChat = document.getElementById('close-chat');
    const sendButton = document.getElementById('send-message');
    const chatInput = document.getElementById('chat-input');
    const chatBody = document.getElementById('chat-body');

    // Open chat: set display to block, then slide in
    messageIcon.addEventListener('click', function() {
        chatDropdown.style.display = 'block';
        setTimeout(() => {
            chatDropdown.classList.add('open');
        }, 10); // Small delay to allow display change before animation
    });

    // Close chat: slide out, then set display to none
    closeChat.addEventListener('click', function() {
        chatDropdown.classList.remove('open');
        setTimeout(() => {
            chatDropdown.style.display = 'none';
        }, 300); // Match the CSS transition duration (0.3s = 300ms)
    });

    // Close chat when clicking outside
    window.addEventListener('click', function(event) {
        if (!chatDropdown.contains(event.target) && event.target !== messageIcon) {
            chatDropdown.classList.remove('open');
            setTimeout(() => {
                chatDropdown.style.display = 'none';
            }, 300);
        }
    });

    // Send message functionality
    sendButton.addEventListener('click', function() {
        const message = chatInput.value.trim();
        if (message) {
            // Add user's message to chat
            const userMessageElem = document.createElement('div');
            userMessageElem.textContent = 'You: ' + message;
            chatBody.appendChild(userMessageElem);

            // Send message to server
            fetch('/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'message=' + encodeURIComponent(message)
            })
            .then(response => response.json())
            .then(data => {
                if (data.response) {
                    // Add bot's response
                    const botMessageElem = document.createElement('div');
                    botMessageElem.textContent = 'Qwen: ' + data.response;
                    chatBody.appendChild(botMessageElem);
                } else {
                    // Add error message
                    const errorElem = document.createElement('div');
                    errorElem.classList.add('error');
                    errorElem.textContent = 'Error: ' + data.error;
                    chatBody.appendChild(errorElem);
                }
                chatBody.scrollTop = chatBody.scrollHeight; // Scroll to bottom
            })
            .catch(error => {
                console.error('Error:', error);
                const errorElem = document.createElement('div');
                errorElem.classList.add('error');
                errorElem.textContent = 'Error: Unable to connect to the server.';
                chatBody.appendChild(errorElem);
            });

            chatInput.value = ''; // Clear input
        }
    });

    // Send message on Enter key press
    chatInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendButton.click();
        }
    });
});