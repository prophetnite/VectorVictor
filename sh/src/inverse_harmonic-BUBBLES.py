#import gensim
import csv
f = file('relationmap.txt', 'w')
sentences = ''
with file('../bubbles/csv/cos_knn_10.csv', 'rb') as csvfile:
    spamreader = csv.reader(csvfile, delimiter=' ', quotechar='|')
    for row in spamreader:
        sentences+='  '.join(row).replace(';',' ')+' '
f.write(str(sentences))
#model = gensim.models.Word2Vec.load('vecmodel.bin', binary=True)
#model = gensim.models.Word2Vec.load('../vecmodel.bin')
#corpus = file('../../enwiki-articles1.xml') #wikipedia latest full article dump
#print(dict(doc).get(123, 0) for doc in corpus)
#converted = []
#for doc in corpus:
#    for word in dict(doc).get(123, 0):
#       print(model.syn0[model.vocab[word].index])
#results=['woman','king','man']
#result = list()
#for result in results:
#    result = list(result) # make a list from the tuple
#for i in range(len(result)):
#    result[i] = str(unicode(result[i], 'utf-8'))
#    converted.append(tuple(result)) # tuplify again
#import copy
#from gensim.models import VocabTransform

# filter the dictionary
#old_dict = corpora.Dictionary.load('old.dict')
#new_dict = copy.deepcopy(old_dict)
#new_dict.filter_extremes(keep_n=100000)
#new_dict.save('filtered.dict')

#default = 'man'
#print(model.create_binary_tree())
#print(model)
#print(model.most_similar(['python', 'gensim'], ['harry potter'], topn=3000))
