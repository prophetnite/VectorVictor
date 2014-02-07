import gensim
import json
from operator import attrgetter
import numpy
import math
#import calc_tsne
import custom_vec
import string
from StringIO import StringIO
import re
sentence = u"This sentence contains a series of words, some of which are stupid; for example: banana, fish, bird, and chimp."
print sentence

sentence_processed = gensim.utils.simple_preprocess(sentence)
sentence_processed = [word for word in sentence_processed if word not in custom_vec.STOPWORDS]
model = gensim.models.Word2Vec.load('../vecmodel-full1000-2.bin')

class Word:
    w = u''
    is_stopword=False
    x = 400
    y = 400
    #vec = numpy.array()
    score = 0
    def __init__(self, w):
        self.w = str(w.encode('utf-8'))#all words in model are utf-8 encoded!
        #self.vec = vec #full vector representation

    '''
        Return the similarity between this word and another word.
        Returns score as a float range(-1,1).
    '''
    def similarity(self, other_word, model):
        return model.most_similar(positive=[self.w,other_word.w],negative=[],topn=1)[0][1]

    def add_score(self, n):
        self.score += n

    def __str__(self):
        return self.w

    def to_dict(self):
        return {self.w: [self.x,self.y,self.score]}

    def strip_punctuation(self,s):
        return s.strip(string.punctuation)

words = []
for word in sentence_processed:
    words.append(Word(word))

for i in xrange(len(words)):
    j = i + 1
    while j < len(words):
        n = words[i].similarity(words[j],model)
        words[i].add_score(n)
        words[j].add_score(n)
        j = j + 1

words_sorted = sorted(words, key=attrgetter('score'))
#for word in words_sorted:
#    print word.w + ' ' + str(word.score)


max_score = words_sorted[0].score
j = len(words_sorted)-1
while j >= 0:
    cur_word = words_sorted[j]
    # produces cluster of coordinates for each word around the most important word
    cur_word.x = 400 + (400 * (max_score - cur_word.score)) - 100*math.sin(j/8.1)
    cur_word.y = 400 + (400 * (max_score - cur_word.score)) - 100*math.cos(j/9.2) #little wider than it is tall
    j = j - 1

print json.dumps({'data':str([word.to_dict() for word in words])})

