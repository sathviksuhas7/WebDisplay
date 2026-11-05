<!DOCTYPE html>
<html>
<head>
    <title>Student Details - AJAX</title>
</head>
<body>

<h1>Student Details Form</h1>

<form id="studentForm">

    Name:
    <input type="text" id="name" required>
    <br><br>

    Roll Number:
    <input type="text" id="roll" required>
    <br><br>

    Age:
    <input type="number" id="age" required>
    <br><br>

    Department:
    <input type="text" id="department" required>
    <br><br>

    <button type="submit">Submit</button>

</form>

<h2>Student Information</h2>

<div id="result"></div>

<script>

document.getElementById("studentForm")
.addEventListener("submit", function(event) {

    event.preventDefault();

    const params = new URLSearchParams();

    params.append("name", document.getElementById("name").value);
    params.append("roll", document.getElementById("roll").value);
    params.append("age", document.getElementById("age").value);
    params.append("department", document.getElementById("department").value);

    fetch("/student", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: params
    })

    .then(response => {
        if (!response.ok) {
            throw new Error("Request failed");
        }
        return response.json();
    })

    .then(data => {

        const result = document.getElementById("result");

        result.innerHTML = "";

        const details = [
            "Name: " + data.name,
            "Roll Number: " + data.roll,
            "Age: " + data.age,
            "Department: " + data.department
        ];

        details.forEach(detail => {
            const p = document.createElement("p");
            p.textContent = detail;
            result.appendChild(p);
        });

    })

    .catch(error => {
        console.log(error);
        document.getElementById("result").innerText =
            "Unable to retrieve student details.";
    });

});



</script>

</body>
</html>