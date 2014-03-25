package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;

import java.util.List;

public class LargeCacheTestCase extends TcpDumpTestCase {

    protected static final String Hamlet = "To be, or not to be, that is the Question:"
            + "Whether tis Nobler in the minde to suffer" + "The Slings and Arrowes of outragious Fortune,"
            + "Or to take Armes against a Sea of troubles," + "And by opposing end them: to dye, to sleepe"
            + "No more; and by a sleepe, to say we end" + "The Heart-ake, and the thousand Naturall shockes"
            + "That Flesh is heyre too? Tis a consummation" + "Deuoutly to be wishd. To dye to sleepe,"
            + "To sleepe, perchance to Dreame; I, theres the rub,"
            + "For in that sleepe of death, what dreames may come," + "When we haue shuffeld off this mortall coile,"
            + "Must giue vs pawse. Theres the respect" + "That makes Calamity of so long life:"
            + "For who would beare the Whips and Scornes of time," + "The Oppressors wrong, the poore mans Contumely,"
            + "The pangs of disprizd Loue, the Lawes delay," + "The insolence of Office, and the Spurnes"
            + "That patient merit of the vnworthy takes," + "When he himselfe might his Quietus make"
            + "With a bare Bodkin? Who would these Fardles beare" + "To grunt and sweat vnder a weary life,"
            + "But that the dread of something after death," + "The vndiscouered Countrey, from whose Borne"
            + "No Traueller returnes, Puzels the will," + "And makes vs rather beare those illes we haue,"
            + "Then flye to others that we know not of." + "Thus Conscience does make Cowards of vs all,"
            + "And thus the Natiue hew of Resolution" + "Is sicklied ore, with the pale cast of Thought,"
            + "And enterprizes of great pith and moment," + "With this regard their Currants turne away,"
            + "And loose the name of Action. Soft you now," + "The faire Ophelia? Nimph, in thy Orizons"
            + "Be all my sinnes remembred";
    protected long sleepTime = 500L;

    protected void largeCacheTest(List<HttpRequest> requests, List<String> uris, HttpRequest request, int countRequest, int[] repeatRequests,
                                  boolean invalidate) throws Throwable {
        long startTime;
        int requestId = 1;
        try {
            if (invalidate) {
                for (int i = 0; i < countRequest; i++) {
                    checkMiss(requests.get(i), requestId++);
                    TestUtil.sleep(200);
                    for (int j = 0; j < repeatRequests[i]; j++) {
                        checkHit(requests.get(i), requestId++);
                        TestUtil.sleep(200);
                    }
                }
                // RMP start on 126 request
                requestId = checkPoll(request, requestId, 1, MIN_NON_RMP_PERIOD);

                startTime = System.currentTimeMillis();

                for (int i = 0; i < countRequest; i++) {
                    checkHit(requests.get(i), requestId++);
                }

                TestUtil.sleep(MIN_NON_RMP_PERIOD - System.currentTimeMillis() + startTime);

                PrepareResourceUtil.prepareDiffResource(request.getUri(), Hamlet);
                TestUtil.sleep(2 * MIN_NON_RMP_PERIOD);
                checkTransient(request, requestId++, startTime);

                for (int i = 0; i < 5; i++) {
                    if (i == 4) {
                        checkMiss(requests.get(i), requestId++);
                    }
                    checkHit(requests.get(i), requestId++);
                }
            } else {
                for (int i = 0; i < countRequest; i++) {
                    request.setUri(uris.get(i));

                    checkMiss(request, requestId++);
                    TestUtil.sleep(sleepTime);

                    checkHit(request, requestId++);
                    TestUtil.sleep(sleepTime);
                }

                request.setUri(uris.get(0));
                checkMiss(request, requestId);
            }
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(request.getUri());
        }
    }
}
