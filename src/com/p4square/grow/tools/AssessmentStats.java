/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.tools;


import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.p4square.grow.model.Answer;
import com.p4square.grow.model.Question;
import com.p4square.grow.model.RecordedAnswer;
import com.p4square.grow.model.Score;
import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.JsonEncodedProvider;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AssessmentStats {
    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: AssessmentStats directory firstQuestionId");
            System.exit(1);
        }

        Map<String, Question> questions;
        questions = loadQuestions(args[0], args[1]);

        // Find the highest possible score
        List<AnswerPath> scores = findHighestFromId(questions, args[1]);

        // Print Results
        System.out.printf("Found %d different paths.\n", scores.size());
        int i = 0;
        for (AnswerPath path : scores) {
            Score s = path.mScore;
            System.out.printf("Path %d: %f points, %d questions. Score: %f (%s)\n",
                    i++, s.getSum(), s.getCount(), s.getScore(), s.toString());
            System.out.println("    " + path.mPath);
            System.out.println("    " + path.mScores);
        }
    }

    private static Map<String, Question> loadQuestions(String baseDir, String firstId) throws IOException {
        FileQuestionProvider provider = new FileQuestionProvider(baseDir);

        // Questions to find...
        Queue<String> queue = new LinkedList<>();
        queue.offer(firstId);

        Map<String, Question> questions = new HashMap<>();


        while (!queue.isEmpty()) {
            Question q = provider.get(queue.poll());
            questions.put(q.getId(), q);

            if (q.getNextQuestion() != null) {
                queue.offer(q.getNextQuestion());

            }

            for (Answer a : q.getAnswers().values()) {
                if (a.getNextQuestion() != null) {
                    queue.offer(a.getNextQuestion());
                }
            }

            // Quick Sanity check
            if (q.getPreviousQuestion() != null) {
                if (questions.get(q.getPreviousQuestion()) == null) {
                    throw new IllegalStateException("Haven't seen previous question??");
                }
            }
        }

        return questions;
    }

    private static List<AnswerPath> findHighestFromId(Map<String, Question> questions, String id) {
        List<AnswerPath> scores = new LinkedList<>();
        doFindHighestFromId(questions, id, scores, new AnswerPath());
        return scores;
    }

    private static void doFindHighestFromId(Map<String, Question> questions, String id, List<AnswerPath> scores, AnswerPath path) {
        if (id == null) {
            // End of the road! Save the score and return.
            scores.add(path);
            return;
        }

        Question q = questions.get(id);

        // Find the best answer following this path and find other paths.
        Score maxScore = path.mScore;
        double max = 0;

        int answerCount = 1;
        for (Map.Entry<String, Answer> entry : q.getAnswers().entrySet()) {
            Answer a = entry.getValue();
            RecordedAnswer userAnswer = new RecordedAnswer();

            if (q.getType() == Question.QuestionType.SLIDER) {
                // Special Case
                userAnswer.setAnswerId(String.valueOf((float) answerCount / q.getAnswers().size()));

            } else {
                userAnswer.setAnswerId(entry.getKey());
            }

            Score tempScore = new Score(path.mScore); // Always start with the initial score.
            boolean endOfRoad = !q.scoreAnswer(tempScore, userAnswer);
            double thisScore = tempScore.getSum() - path.mScore.getSum();

            if (endOfRoad) {
                // End of Road is a fork too. Record and pick another answer.
                AnswerPath fork = new AnswerPath(path);
                fork.update(id, tempScore);
                scores.add(fork);

            } else if (a.getNextQuestion() != null) {
                // Found a new path, follow it.
                // Remember to count this answer in the score.
                AnswerPath fork = new AnswerPath(path);
                fork.update(id, tempScore);
                doFindHighestFromId(questions, a.getNextQuestion(), scores, fork);

            } else if (thisScore > max) {
                // Found a higher option that isn't a new path.
                maxScore = tempScore;
                max = thisScore;
            }

            answerCount++;
        }

        path.update(id, maxScore);
        doFindHighestFromId(questions, q.getNextQuestion(), scores, path);
    }

    private static class FileQuestionProvider extends JsonEncodedProvider<Question> implements Provider<String, Question> {
        private String mBaseDir;

        public FileQuestionProvider(String directory) {
            super(Question.class);
            mBaseDir = directory;
        }

        @Override
        public Question get(String key) throws IOException {
            Path qfile = FileSystems.getDefault().getPath(mBaseDir, key + ".json");
            byte[] blob = Files.readAllBytes(qfile);
            return decode(new String(blob));
        }

        @Override
        public void put(String key, Question obj) throws IOException {
            throw new UnsupportedOperationException("Not Implemented");
        }
    }

    private static class AnswerPath {
        String mPath;
        String mScores;
        Score mScore;

        public AnswerPath() {
            mPath = null;
            mScores = null;
            mScore = new Score();
        }

        public AnswerPath(AnswerPath other) {
            mPath = other.mPath;
            mScores = other.mScores;
            mScore = other.mScore;
        }

        public void update(String questionId, Score newScore) {
            String value;

            if (mScore.getCount() == newScore.getCount()) {
                value = "n/a";

            } else {
                double delta = newScore.getSum() - mScore.getSum();
                if (delta < 0) {
                    value = "TRUMP";
                } else {
                    value = String.valueOf(delta);
                }
            }

            if (mPath == null) {
                mPath = questionId;
                mScores = value;

            } else {
                mPath += ",  " + questionId;
                mScores += " + " + value;
            }

            mScore = newScore;
        }
    }
}
