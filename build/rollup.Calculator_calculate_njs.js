import {terser} from 'rollup-plugin-terser';

export default {
  input: 'src/api/cms/Calculator_calculate.es',
  output: {
    file: 'WEB-INF/classes/api/cms/Calculator_calculate.njs',
    format: 'es',
    plugins: [terser()]
  },
  external: ['vue']
};
