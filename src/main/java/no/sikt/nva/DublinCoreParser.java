package no.sikt.nva;

import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.publisher.Publication;
import no.sikt.nva.model.record.Record;

@SuppressWarnings({"PMD.CollapsibleIfStatements", "PMD.CognitiveComplexity"})
public class DublinCoreParser {

    public static final String HAS_CRISTIN_ID_MESSAGE_TEMPLATE =
        "Following resource has Cristin identifier: %s, this error "
        + "occurred in: %s";

    public Record convertDublinCoreToRecord(DublinCore dublinCore, Record record) {

        hasCristinIdentifier(record, dublinCore);
        setPublication(record, dublinCore);
        setAuthors(record, dublinCore);
        setTitle(record, dublinCore);
        setLanguage(record, dublinCore);
        setType(record, dublinCore);

        return record;
    }

    private void setPublisher(Publication publication, DublinCore dublinCore) {
        var publisher = dublinCore.getDcValues().stream()
                            .filter(DcValue::isPublisher)
                            .findAny().orElse(new DcValue()).getValue();

        publication.setPublisher(publisher);
    }

    private void setPublication(Record record, DublinCore dublinCore) {
        var publication = new Publication();
        setIssn(publication, dublinCore);
        setPublisher(publication, dublinCore);
        setJournal(publication, dublinCore);

        record.setPublication(publication);
    }

    private void setJournal(Publication publication, DublinCore dublinCore) {
        var journal = dublinCore.getDcValues().stream()
                          .filter(DcValue::isJournal)
                          .findAny().orElse(new DcValue()).getValue();

        publication.setJournal(journal);
    }

    private void setAuthors(Record record, DublinCore dublinCore) {
        var authors = dublinCore.getDcValues().stream()
                          .filter(DcValue::isAuthor)
                          .map(DcValue::getValue)
                          .collect(Collectors.toList());

        record.setAuthors(authors);
    }

    private void setType(Record record, DublinCore dublinCore) {
        var type = dublinCore.getDcValues().stream()
                       .filter(DcValue::isType)
                       .findAny().orElse(new DcValue()).getValue();
        record.setType(type);
    }

    private void setLanguage(Record record, DublinCore dublinCore) {
        var language = dublinCore.getDcValues().stream()
                           .filter(DcValue::isLanguage)
                           .findAny().orElse(new DcValue()).getValue();
        record.setLanguage(language);
    }

    private void setTitle(Record record, DublinCore dublinCore) {
        var title = dublinCore.getDcValues().stream()
                        .filter(DcValue::isTitle)
                        .findAny().orElse(new DcValue()).getValue();
        record.setTitle(title);
    }

    private void setIssn(Publication publication, DublinCore dublinCore) {
        var issn = dublinCore.getDcValues().stream()
                       .filter(DcValue::isIssnValue)
                       .findAny().orElse(new DcValue()).getValue();

        publication.setIssn(issn);
    }

    private void hasCristinIdentifier(Record record, DublinCore dublinCore) {
        var cristinIdentifier = dublinCore.getDcValues().stream()
                                    .filter(DcValue::isCristinDcValue)
                                    .findAny();

        if (cristinIdentifier.isPresent()) {
            throw new DublinCoreException(String.format(HAS_CRISTIN_ID_MESSAGE_TEMPLATE, cristinIdentifier.get(),
                                                        record.getOriginInformation()));
        }
    }
}
