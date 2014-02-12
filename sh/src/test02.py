'''
Created on Nov 8, 2013

@author: erik
'''
import gensim, gzip



#model = gensim.models.Word2Vec.load_word2vec_format(('../../gn-300.bin.gz'), binary=True)
model = gensim.models.Word2Vec.load('../vecmodel-full1000.bin')

converted = []
stoplist = set('for a of the and to in'.split())
topic = u'requirements'
results=gensim.utils.simple_preprocess(u"the requirements document is nonlinear with a variety of internal links")
results = [word for word in results if word not in stoplist]

for result in results:
    result = str(result)
    converted.append(result)
print(converted)

outlier = model.doesnt_match(results)
best_word = ['',-1]
worst_word = ['',1]
print('OUTLIER: ' + outlier)
score = 1
for i in range(len(results)):

    sim = model.similarity(results[i], topic)
    print('score of: ' + str(converted[i]) + ' ' + str(sim))
    print(model.most_similar(positive=[converted[i], topic], negative=[outlier], topn=3))
    if(sim>best_word[1]):
        best_word[0]=converted[i]
        best_word[1]=sim
    if(sim<worst_word[1]):
        worst_word[0]=converted[i]
        worst_word[1]=sim
    score += sim*10


print('best word: ' + str(best_word))
print('worst word: ' + str(worst_word))

print('total score: ' + str(score/len(results)))

#for i in range(10):
#    print(model.index2word[i])
