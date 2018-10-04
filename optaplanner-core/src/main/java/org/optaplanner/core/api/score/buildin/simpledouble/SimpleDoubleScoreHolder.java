/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.score.buildin.simpledouble;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;

/**
 * WARNING: NOT RECOMMENDED TO USE DUE TO ROUNDING ERRORS THAT CAUSE SCORE CORRUPTION.
 * Use {@link SimpleDoubleScoreHolder} instead.
 * @see SimpleDoubleScore
 */
public class SimpleDoubleScoreHolder extends AbstractScoreHolder<SimpleDoubleScore> {

    protected final Map<Rule, BiConsumer<RuleContext, Double>> matchExecutorMap = new LinkedHashMap<>();

    protected double score;

    public SimpleDoubleScoreHolder(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, SimpleDoubleScore.ZERO);
    }

    public double getScore() {
        return score;
    }

    // ************************************************************************
    // Setup methods
    // ************************************************************************

    @Override
    public void putConstraintWeight(Rule rule, SimpleDoubleScore constraintWeight) {
        BiConsumer<RuleContext, Double> matchExecutor;
        if (constraintWeight.equals(SimpleDoubleScore.ZERO)) {
            matchExecutor = (RuleContext kcontext, Double matchWeight) -> {};
        } else if (constraintWeight.getInitScore() != 0) {
            throw new IllegalStateException("The initScore (" + constraintWeight.getInitScore() + ") must be 0.");
        } else {
            matchExecutor = (RuleContext kcontext, Double matchWeight)
                    -> addConstraintMatch(kcontext, constraintWeight.getScore() * matchWeight);
        }
        matchExecutorMap.put(rule, matchExecutor);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param weight higher is better, negative for a penalty, positive for a reward
     */
    public void addConstraintMatch(RuleContext kcontext, double weight) {
        score += weight;
        registerConstraintMatch(kcontext,
                () -> score -= weight,
                () -> SimpleDoubleScore.valueOf(weight));
    }

    @Override
    public SimpleDoubleScore extractScore(int initScore) {
        return SimpleDoubleScore.valueOfUninitialized(initScore, score);
    }

}
