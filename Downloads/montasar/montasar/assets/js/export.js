document.getElementById("exportbutton").addEventListener("click", function () {
    let table = document.querySelector("table"); // Sélectionne le tableau
    let rows = table.querySelectorAll("tr");
    let csvContent = "";

    rows.forEach(row => {
        let cols = row.querySelectorAll("th, td");
        let rowData = [];
        cols.forEach(col => rowData.push(col.innerText));
        csvContent += rowData.join(",") + "\n";
    });

    let blob = new Blob([csvContent], { type: "text/csv" });
    let link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "export.csv";
    link.click();
});
