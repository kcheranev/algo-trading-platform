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