import logging,gensim
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
mm = gensim.corpora.MmCorpus('../enwiki_tfidf.mm.index')
print mm
