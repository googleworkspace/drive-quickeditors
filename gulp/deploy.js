var deploy = require('gulp-gh-pages');
var gulp = require('gulp');

gulp.task('deploy', function () {
    return gulp.src('./dist/**/*')
        .pipe(deploy({}));
});
