import logging
import os
from numpy import exp, dot, zeros, outer, random, dtype, get_include, float32 as REAL,\
    uint32,int64, seterr, array, uint8, vstack, argsort, fromstring, sqrt, newaxis, ndarray, float64
import gensim, re,string

logger = logging.getLogger("gensim.models.word2vec")
STOPWORDSS = u"""
a about above across after afterwards again against all almost alone along already also although always am among amongst amoungst amount an and another any anyhow anyone anything anyway anywhere are around as at back be
became because become becomes becoming been before beforehand behind being below beside besides between beyond bill both bottom but by call can
cannot cant co computer con could couldnt cry de describe
detail did do doesn done down due during
each eg eight either eleven else elsewhere empty enough etc even ever every everyone everything everywhere except few fifteen
fify fill find fire first five for former formerly forty found four from front full further get give go
had has hasnt have he hence her here hereafter hereby herein hereupon hers herself him himself his how however hundred i ie
if in inc indeed interest into is it its itself keep last latter latterly least less ltd
just
kg km
made many may me meanwhile might mill mine more moreover most mostly move much must my myself name namely
neither never nevertheless next nine no nobody none noone nor not nothing now nowhere of off
often on once one only onto or other others otherwise our ours ourselves out over own part per
perhaps please put rather re
quite
rather really regarding
same see seem seemed seeming seems serious several she should show side since sincere six sixty so some somehow someone something sometime sometimes somewhere still such system take ten
than that the their them themselves then thence there thereafter thereby therefore therein thereupon these they thick thin third this those though three through throughout thru thus to together too top toward towards twelve twenty two un under
until up unless upon us used using
various very very via
was we well were what whatever when whence whenever where whereafter whereas whereby wherein whereupon wherever whether which while whither who whoever whole whom whose why will with within without would yet you
your yours yourself yourselves
"""
STOPWORDS = frozenset(w for w in STOPWORDSS.split() if w)

class CustomVec(object) :
    def __init__(self, dirname):
         self.dirname = dirname

    @classmethod
    def load_w2v(cls, fname, numLines=0, binary=True): #added numlines for partial model loading
        logger.info("loading projection weights from %s" % (fname))
        with open(fname) as fin:
            header = fin.readline()
            vocab_size, layer1_size = map(int, header.split())  # throws for invalid file format
            layer1_size=400
            result = gensim.models.Word2Vec(size=300)
            result.syn0 = zeros((vocab_size, layer1_size), dtype=REAL)
            if binary:
                binary_len = dtype(int64).itemsize * layer1_size

                for line_no in xrange(vocab_size if numLines==0 else numLines):
                    # mixed text and binary: read text first, then binary
                    word = []
                    while True:
                        ch = fin.read(1)
                        if ch == ' ':
                            word = ''.join(word)
                            break
                        if ch != '\n':  # ignore newlines in front of words (some binary files have newline, some not)
                            word.append(ch)
                    result.vocab[word] = gensim.models.word2vec.Vocab(index=line_no, count=vocab_size - line_no)
                    result.index2word.append(word)
                    result.syn0[line_no] = fromstring(fin.read(binary_len), dtype=int64)
            else:
                for line_no, line in enumerate(fin):
                    parts = line.split()
                    assert len(parts) == layer1_size + 1
                    word, weights = parts[0], map(REAL, parts[1:])
                    result.vocab[word] = gensim.models.word2vec.Vocab(index=line_no, count=vocab_size - line_no)
                    result.index2word.append(word)
                    result.syn0[line_no] = weights
        logger.info("loaded %s matrix from %s" % (result.syn0.shape, fname))
        result.init_sims()
        return result

    def _process_article(self, document):
        text = gensim.corpora.wikicorpus.filter_wiki(document) # remove markup, get plain text
        return gensim.corpora.wikicorpus.tokenize(text) # tokenize plain text

    def convert_wiki(self, infile):

        #Yield articles from Wikipedia dump 'infile'
        texts = gensim.corpora.wikicorpus._extract_pages(bz2.BZ2File(infile)) # generator
        for text in texts:
            for tokens in _process_article(text):
                if len(tokens) >= 50 and not tokens[0].startswith('Wikipedia:'):
                    yield tokens

    def augment(self):
        pass

    def is_number(s):
        try:
            float(s)
            return True
        except ValueError:
            return False

    def proc_doc(self, doc):
        q = []
        for s in doc.readlines():
            q.append(self.remove_stopwords(self._process_article(self.strip_punctuation(s))))
        return q

    def remove_stopwords(self,s):
        return [w for w in s if w not in STOPWORDS]


    def strip_punctuation(self,s):
        return re.sub("([%s]+)" % string.punctuation, " ", s).translate(string.maketrans("", ""), string.punctuation)


    def strip_tags(self,s):
        # assumes s is already lowercase
        return re.sub(r"<([^>]+)>", "", s)
