VocabTrain - a framework for spaced repetitive, collaborative learning

We present a new framework that seeks to aid self studying material for learning, 
based on spaced repetition [RepRev]. 
The information that has to be learned is split into information entities and materialized as flash cards [Leitner].
Common learning software [LearnSoftware] are based on the Leitner system [Leitner].
This system assumes a linear progress in retention time.
Furthermore, whenever an information entity gets forgotten the progress shall be reset and start from the beginning.
Unfortunately, several studies [Rohrer] show that best retention is achieved by an exponential scaling of the review intervals.
The Satisficing Spaced Repetition Formula [SRFF] proposes a new model that seeks to fit better to the true psychological background.
According to SRFF, each flash card gets a numerical grade by the student.
More precisely, retention interval computation is influenced by
the number of reviews, the average grade, the current grade and a individual set priority.
In particular, the model assumes that information about forgotten cards still remains to some extent in the memory.
Thus the learning history never gets erased.
Instead, a current bad grade will shorten the review interval, 
but a good grading on the next review will enlarge the interval much more than the same grading for a very new card.

Secondly, we highlight aspects of collaborative work-flows.
Different lessons and textbooks of the same topic share basically the same information context (but, e.g., with different emphasizes).
Instead of grouping information based on their origin distinctly,
we propose a schema that deals with information interactions, i.e., 
we build an inverse index that tells us the sources of an information entity.
The idea is that learners can refer to already present information contexts instead of re-creating 
the same entities over and over again.
Furthermore, learners can detect mistakes of former co-learners while transferring information to the flash cards.

Unfortunately, current learning software do still neglect more sophisticated models like SRFF 
and are unaware of collaborative aspects of learning.


[RepRev] Greene R. L. (2008). Repetition and spacing effects. In Roediger H. L. III (Ed.), Learning and memory: A comprehensive reference. Vol. 2: Cognitive psychology of memory (pp. 65–78). Oxford: Elsevier.

[Leitner] So lernt man lernen. Der Weg zum Erfolg (How to learn to learn), Freiburg i. Br. 1972/2003, ISBN 3-451-05060-9

[LearnSoftware] Baker, Stephen (2011). Final Jeopardy: Man vs. Machine and the Quest to Know Everything. Houghton Mifflin Harcourt. p. 214. ISBN 978-0-547-48316-0

[SSRF] Dariusz Laska, Komputerowe wspomaganie efektywności procesu nauczania metodą spaced repetition,
Kraków 2010, Magister thesis

[Rohrer] http://www.pashler.com/Articles/RohrerPashler2007CDPS.pdf

