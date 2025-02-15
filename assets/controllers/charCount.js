document.addEventListener('DOMContentLoaded', () => {
    const textarea = document.getElementById('legende');
    const charCount = document.getElementById('charCount');

    if (textarea && charCount) {
        textarea.addEventListener('input', () => {
            const maxLength = 1000;
            const remaining = maxLength - textarea.value.length;
            charCount.textContent = remaining;

            if (remaining < 0) {
                charCount.style.color = 'red';
            } else {
                charCount.style.color = 'black';
            }
        });

        // Déclenche l'événement 'input' au chargement
        textarea.dispatchEvent(new Event('input'));
    } else {
        console.error("Éléments non trouvés : vérifie les ID 'legende' et 'charCount'.");
    }
});