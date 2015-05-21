package org.deeplearning4j.rbm;

import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.override.ClassifierOverride;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RBMCreateDataExample {

    private static Logger log = LoggerFactory.getLogger(RBMCreateDataExample.class);

    public static void main(String... args) throws Exception {
        int numFeatures = 614;

        log.info("Load data....");
        //        MersenneTwister gen = new MersenneTwister(123); // other data to test?

        INDArray input = Nd4j.create(2, numFeatures); // have to be at least two or else output layer gradient is a scalar and cause exception
        INDArray labels = Nd4j.create(2, 2);

        INDArray row0 = Nd4j.create(1, numFeatures);
        row0.assign(0.1);
        input.putRow(0, row0);
        labels.put(0, 1, 1); // set the 4th column

        INDArray row1 = Nd4j.create(1, numFeatures);
        row1.assign(0.2);

        input.putRow(1, row1);
        labels.put(1, 0, 1); // set the 2nd column

        DataSet trainingSet = new DataSet(input, labels);

        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .layer(new RBM()).nIn(trainingSet.numInputs()).nOut(trainingSet.numOutcomes())
                .weightInit(WeightInit.SIZE).iterations(3)
                .activationFunction("tanh")
                .visibleUnit(org.deeplearning4j.nn.conf.layers.RBM.VisibleUnit.GAUSSIAN)
                .hiddenUnit(org.deeplearning4j.nn.conf.layers.RBM.HiddenUnit.RECTIFIED)
                .lossFunction(LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY)
                .learningRate(1e-1f)
                .optimizationAlgo(OptimizationAlgorithm.ITERATION_GRADIENT_DESCENT)
                .list(2).hiddenLayerSizes(new int[]{400})
                .override(new ClassifierOverride(3)).build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);

        log.info("Train model....");
        model.fit(trainingSet);

        log.info("Evaluate model....");
        INDArray predict2 = model.output(trainingSet.getFeatureMatrix());
        for (int i = 0; i < predict2.rows(); i++) {
            String actual = trainingSet.getLabels().getRow(i).toString().trim();
            String predicted = predict2.getRow(i).toString().trim();
            log.info("actual " + actual + " vs predicted " + predicted);
        }

        Evaluation eval = new Evaluation();
        eval.eval(trainingSet.getLabels(), predict2);
        log.info(eval.stats());

        log.info("****************Example finished********************");

    }
}