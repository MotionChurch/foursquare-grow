function notice(msg)
{
    alert(msg);
}

function selectAnswer(element)
{
    $(element).siblings('.selected').removeClass('selected');
    $(element).addClass('selected');
    $("#answerField").val(element.id);
}

function nextQuestion()
{
    var selectedAnswer = $("#answerField").val();
    
    if (selectedAnswer == '') {
        notice('Please select an answer before moving to the next question');
        return;
    }

    $("#questionForm").submit();
}
