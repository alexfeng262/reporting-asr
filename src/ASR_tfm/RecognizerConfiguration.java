
package ASR_tfm;

import asr_utils.Resource_manager;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import java.util.Map;

/**
 *
 * @author alexf
 */
public class RecognizerConfiguration {
    private static double RelBeamWidth;
    private static double Wip;   
    private static double Lw;   
    private static double Pbeam;
    public RecognizerConfiguration(){
        Resource_manager props = new Resource_manager();
        getGlobalProperties(props);
    }
    
    private static void getGlobalProperties(Resource_manager props){
        ConfigurationManager cm;
        cm = new ConfigurationManager(props.getConfig_xml_path());
        
        Map<String, String> global_prop = cm.getGlobalProperties();
        
        RelBeamWidth = Double.parseDouble(global_prop.get("relativeBeamWidth"));
        Wip = Double.parseDouble(global_prop.get("wordInsertionProbability"));
        Lw = Double.parseDouble(global_prop.get("languageWeight"));
        Pbeam = Double.parseDouble(global_prop.get("phoneticBeam"));
        
        
    }
    
    public double getRelBeamWidth(){
        return RelBeamWidth;
    }
    public double getWip(){
        return Wip;
    }
    public double getLw(){
        return Lw;    
    }
    public double getPbeam(){
        return Pbeam;    
    }
    
}
