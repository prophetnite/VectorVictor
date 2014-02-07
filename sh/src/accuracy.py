import gensim
#from custom_vec import CustomVec
import logging
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
#model = gensim.models.Word2Vec([line.split() for line in file('../questions-words.txt').readlines()], size=50, window=5, min_count=2, workers=4)
#model = gensim.models.Word2Vec.load_word2vec_format('../../GoogleNews-vectors-negative300.bin',binary=True)
#model = CustomVec.load_w2v('../../GoogleNews-vectors-negative300.bin',numLines=4000,binary=True)
model = gensim.models.Word2Vec.load('../vecmodel-partial-400.bin.bz2')
#model.train([line.split() for line in open('../questions-words.txt').readlines()])
for s in model.accuracy('../questions-words.txt', restrict_vocab=20000):
    print s
#model.save('questions-model.bin')
