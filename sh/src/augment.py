import gensim, bz2

augment_file = bz2.BZ2File('../../enwiki-articles2.xml.bz2') #wikipedia article dump
model = gensim.models.Word2Vec.load('../vecmodel-full1000.txt')
documents = gensim.corpora.wikicorpus._extract_pages(augment_file)
tokens=[]
for document in documents:
    sentence = gensim.corpora.wikicorpus.filter_wiki(str(document))
    tokens.append(gensim.corpora.wikicorpus.tokenize(sentence.encode('utf-8')))
print(tokens[15])
model.build_vocab(tokens)
model.train(tokens)
model.save('../vecmodel-full1000.txt')
