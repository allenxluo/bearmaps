package bearmaps.utils.graph;

import bearmaps.utils.pq.MinHeapPQ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import edu.princeton.cs.algs4.Stopwatch;

public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {

    private SolverOutcome outcome;
    private List<Vertex> solution;
    private double solutionWeight;
    private int numStatesExplored;
    private double explorationTime;

    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex end, double timeout) {
        MinHeapPQ<Vertex> pq = new MinHeapPQ<>();
        HashMap<Vertex, Double> distTo = new HashMap<>();
        HashMap<Vertex, Vertex> edgeTo = new HashMap<>();
        Stopwatch stopWatch = new Stopwatch();
        solution = new ArrayList<>();

        pq.insert(start, input.estimatedDistanceToGoal(start, end));
        distTo.put(start, 0.0);

        while (true) {
            if (pq.size() == 0) {
                outcome = SolverOutcome.UNSOLVABLE;
                solutionWeight = 0;
                break;
            }
            Vertex v = pq.poll();
            numStatesExplored += 1;
            if (v.equals(end)) {
                outcome = SolverOutcome.SOLVED;
                Vertex curr = end;
                while (true) {
                    solution.add(curr);
                    if (curr.equals(start)) {
                        Collections.reverse(solution);
                        break;
                    }
                    curr = edgeTo.get(curr);
                }
                solutionWeight = distTo.get(end);
                break;
            }
            if (stopWatch.elapsedTime() >= timeout) {
                outcome = SolverOutcome.TIMEOUT;
                solutionWeight = 0;
                break;
            }
            for (WeightedEdge e : input.neighbors(v)) {
                Vertex p = (Vertex) e.from();
                Vertex q = (Vertex) e.to();
                double w = e.weight();
                if (!distTo.containsKey(q) || distTo.get(p) + w < distTo.get(q)) {
                    distTo.put(q, distTo.get(p) + w);
                    edgeTo.put(q, p);
                    if (!pq.contains(q)) {
                        pq.insert(q, distTo.get(q) + input.estimatedDistanceToGoal(q, end));
                    } else {
                        pq.changePriority(q, distTo.get(q) + input.estimatedDistanceToGoal(q, end));
                    }
                }
            }
        }
        explorationTime = stopWatch.elapsedTime();
    }

    public SolverOutcome outcome() {
        return outcome;
    }
    public List<Vertex> solution() {
        return solution;
    }
    public double solutionWeight() {
        return solutionWeight;
    }
    public int numStatesExplored() {
        return numStatesExplored;
    }
    public double explorationTime() {
        return explorationTime;
    }
}
