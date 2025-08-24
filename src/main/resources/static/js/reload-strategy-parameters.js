function changeStrategyTypeListener() {
    return () => {
        const form = document.querySelector("#analyzeForm");
        const formData = new FormData(form);
        formData.append("reloadStrategyParameters", "")
        fetch(form.action, {
            method: "POST",
            body: formData
        }).then(response => {
            response.text()
                .then(responseText => {
                    const htmlPage = new DOMParser().parseFromString(responseText, "text/html");
                    document.querySelector("#strategyParameters")
                        .replaceWith(htmlPage.querySelector("#strategyParameters"));
                });
        });
    }
}

document.querySelector("#strategyType")
    .addEventListener("change", changeStrategyTypeListener());

const brokerInputs = document.getElementById("brokerInputFields").querySelectorAll("select, input");
const fileInputs = document.getElementById("fileInputFields").querySelectorAll("select, input");

const radioBrokerInput = document.getElementById("candlesSeriesBrokerOption");
const radioFileInput = document.getElementById("candlesSeriesFileOption");

function updateFieldStates() {
  if (radioBrokerInput.checked) {
    brokerInputs.forEach(input => input.disabled = false);
    fileInputs.forEach(input => input.disabled = true);
  } else if (radioFileInput.checked) {
    brokerInputs.forEach(input => input.disabled = true);
    fileInputs.forEach(input => input.disabled = false);
  }
}

radioBrokerInput.addEventListener("change", updateFieldStates);
radioFileInput.addEventListener("change", updateFieldStates);

updateFieldStates();