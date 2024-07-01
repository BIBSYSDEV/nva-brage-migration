package no.sikt.nva.scrapers;

import java.util.HashSet;
import java.util.Set;
import no.sikt.nva.brage.migration.common.model.record.PrioritizedProperties;
import no.sikt.nva.channelregister.ChannelRegister;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.JacocoGenerated;

public final class PrioritizeField {

    private static final Set<String> DEGREE_PRIORITIZED_FIELDS =
        Set.of(PrioritizedProperties.ALTERNATIVE_ABSTRACTS.getValue(),
               PrioritizedProperties.ABSTRACT.getValue(),
               PrioritizedProperties.ALTERNATIVE_TITLES.getValue(),
               PrioritizedProperties.MAIN_TITLE.getValue(),
               PrioritizedProperties.PUBLISHER.getValue(),
               PrioritizedProperties.FUNDINGS.getValue(),
               PrioritizedProperties.TAGS.getValue(),
               PrioritizedProperties.REFERENCE.getValue()
               );

    @JacocoGenerated
    private PrioritizeField(){

    }

    public static Set<String> getPrioritizedFields(DublinCore dublinCore, String customer){
        var prioritizedProperties = new HashSet<String>();
        if (ChannelRegister.isDegreeFromInstitutionIssuingDegrees(dublinCore, customer)){
            prioritizedProperties.addAll(DEGREE_PRIORITIZED_FIELDS);
        }
        return prioritizedProperties;

    }


}
