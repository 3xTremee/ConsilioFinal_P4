package org.example.ast;

public class ProgramNode extends AstNode {
    private final DomainNode domain;
    private final ProblemNode problem;

    public ProgramNode(DomainNode domain, ProblemNode problem) {
        this.domain = domain;
        this.problem = problem;
    }

    //getters
    public DomainNode getDomain() {
        return domain;
    }

    public ProblemNode getProblem() {
        return problem;
    }

    @Override
    public String toString() {
        return "Program: \nDomain: " + domain + "\nProblem: " + problem;
    }
}
