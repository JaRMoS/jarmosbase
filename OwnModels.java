/**
 * \page jarmos_ownmodels Including own models to JaRMoS
 * 
 * You can include your own models into JaRMoS, if you have a compatible model whose composition is covered by JaRMoS and \ref jrb or \ref jkermor, respectively.
 * 
 * Most importantly, the interface for communication with JaRMoS is the \subpage jarmos_modelxsd.
 * Next, all the files have to be stored in binary format. Depending on the quantity, this must be:
 * 
 * \li Vectors: The first four bytes encode an integer, denoting the length n of the vector.
 * The next 8*n bytes are binary representations (IEEE standard) of double values.
 * \li Matrices: The first eight bytes encode two integers, denoting the number n of rows and m of columns of the matrix.
 * The next 8*n*m bytes are binary representations (IEEE standard) of double values, where the matrices are stored row-wise.
 * 
 * For export of these binary files, the software framework
 * 
 * \section JRB
 * 
 * 
 * 
 * \note Developer information: The main model loading routine is rb.RBContainer#loadModel, which takes a jarmos.io.AModelManager instance to load the model-dependent data.
 * You can also refer to the source code in order to reverse-engineer the necessary files :-)
 * 
 * \section JKerMor
 * 
 * The model import for JKerMor models is still under development.
 */
