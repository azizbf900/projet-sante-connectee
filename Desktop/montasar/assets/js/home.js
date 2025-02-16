const buttonDash1 = document.getElementById("dash1");
const buttonDash2 = document.getElementById("dash2");
const grid_container = document.getElementById("body");
const a_module = document.getElementById("a_module");
const ul_eva = document.getElementById("ul_eva");
const logo = document.getElementById("logo_m");
const menu = document.getElementById("menu");


buttonDash1.addEventListener("click", () => {
    a_module.style.transitionDuration = "0.5s";
    ul_eva.style.transitionDuration = "0.5s";
    logo.style.transitionDuration = "0.5s";
    menu.style.transitionDuration = "0.5s";
    grid_container.classList.toggle("display");
    a_module.classList.toggle("display");
    ul_eva.classList.toggle("display");
    logo.classList.toggle("display");
    menu.classList.toggle("slide-animation");
    logo.classList.remove("displaylogo");
    buttonDash1.style.display = "none";
    buttonDash2.style.display = "block";
    buttonDash2.style.animation = "fadeIn 0.5s ease-in-out";
});
buttonDash2.addEventListener("click", () => {
    a_module.style.transitionDuration = "0.3s";
    ul_eva.style.transitionDuration = "0.3s";
    logo.style.transitionDuration = "0.3s";
    menu.style.transitionDuration = "0.3s";
    grid_container.classList.toggle("display");
    a_module.classList.toggle("display");
    ul_eva.classList.toggle("display");
    logo.classList.toggle("display");
    menu.classList.toggle("slide-animation");
    logo.classList.add("displaylogo");
    buttonDash1.style.display = "block";
    buttonDash2.style.display = "none";
    buttonDash1.style.animation = "fadeIn 0.3s ease-in-out";
});

const style = document.createElement('style');
style.innerHTML = `
@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}
@keyframes slideAnimation {
    0% { transform: translateX(-100%); opacity: 0; }
    100% { transform: translateX(0); opacity: 1; }
}
#menu.slide-animation {
    animation: slideAnimation 0.5s ease-in-out;
}
`;
document.head.appendChild(style);

function filterQuizzes() {
    const input = document.getElementById('searchbar');
    const filter = input.value.toLowerCase();
    const tableBody = document.getElementById('quizTableBody');
    const rows = tableBody.getElementsByTagName('tr');
    const suggestions = new Set();
    for (let i = 0; i < rows.length; i++) {
        const cells = rows[i].getElementsByTagName('td');
        let rowMatch = false;
        for (let j = 0; j < cells.length; j++) {
            if (cells[j]) {
                const cellValue = cells[j].textContent || cells[j].innerText;
                const lowerValue = cellValue.toLowerCase();
                
                if (lowerValue.includes(filter)) {
                    rowMatch = true;
                    cellValue.split(' ').forEach(word => {
                        if (word.toLowerCase().includes(filter)) {
                            suggestions.add(word);
                        }
                    });
                }
            }
        }
        rows[i].style.display = rowMatch ? "" : "none";
    }
    const autocompleteList = document.getElementById('autocomplete-list');
    autocompleteList.classList.toggle("display");
    autocompleteList.innerHTML = '';
    suggestions.forEach(suggestion => {
        const div = document.createElement('div');
        div.innerHTML = suggestion;
        div.addEventListener('click', function() {
            input.value = suggestion;
            filterQuizzes();
            autocompleteList.innerHTML = '';
        });
        autocompleteList.appendChild(div);
    });

    if (filter === '' || suggestions.size === 0) {
        autocompleteList.innerHTML = '';
    }
}
document.addEventListener('click', function(e) {
    if (!e.target.closest('.autocomplete')) {
        document.getElementById('autocomplete-list').innerHTML = '';
    }
});