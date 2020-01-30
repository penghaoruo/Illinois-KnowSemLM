import lbl_mp as lbl
import gensim

fname = "../SRL_DOC.txt"
sentences = gensim.models.word2vec.LineSentence(fname)

model = lbl.LBL()
model.prepare_vocabulary(sentences)
model.initialise()
model.train(sentences, alpha = 0.001, min_alpha = 0.001, batches = 1000, workers = 8)
model.save()
