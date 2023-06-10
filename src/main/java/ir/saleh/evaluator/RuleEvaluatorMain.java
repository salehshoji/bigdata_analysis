package ir.saleh.evaluator;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class RuleEvaluatorMain {
    public static void main(String[] args) {

        final Yaml yaml = new Yaml(new Constructor(RuleEvaluatorConf.class));
        InputStream inputStream = RuleEvaluator.class.getClassLoader()
                .getResourceAsStream("configs/rule-evaluator.yml");
        RuleEvaluatorConf ruleEvaluatorconf = yaml.load(inputStream);


    }
}
