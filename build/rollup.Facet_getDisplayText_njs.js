import {terser} from 'rollup-plugin-terser';

export default {
  input: 'src/api/cms/Facet_getDisplayText.es',
  output: {
    file: 'WEB-INF/classes/api/cms/Facet_getDisplayText.njs',
    format: 'es',
    plugins: [terser()]
  },
  external: ['vue']
};
